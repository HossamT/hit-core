package gov.nist.hit.core.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import gov.nist.hit.core.domain.AddOrUpdateRequest;
import gov.nist.hit.core.domain.CFTestStep;
import gov.nist.hit.core.domain.ResourceUploadStatus;
import gov.nist.hit.core.domain.TestCase;
import gov.nist.hit.core.domain.TestCaseGroup;
import gov.nist.hit.core.domain.TestPlan;
import gov.nist.hit.core.domain.TestStep;
import gov.nist.hit.core.service.ResourceLoader;
import gov.nist.hit.core.service.exception.NotFoundException;
import gov.nist.hit.core.service.exception.ProfileParserException;
import gov.nist.hit.core.service.util.ResourcebundleHelper;

@RestController
@RequestMapping("/editResources")
// @Api(value = "Add or Update", tags = "Add or Update resources", position =
// 10)
public class ResourceController {

	@Autowired
	private ResourceLoader resourceLoader;

	@ModelAttribute("requestObj")
	public AddOrUpdateRequest initRequest(@RequestPart("file") MultipartFile file, @RequestPart("id") Long id)
			throws Exception {
		AddOrUpdateRequest request = new AddOrUpdateRequest();
		if (!file.isEmpty()) {
			String zipFolder = ResourcebundleHelper.getResourcesFromZip(file.getInputStream());
			request.setZip(zipFolder);
		}
		request.setId(id);
		return request;
	}

	// Context Based API Methods

	// Test Step

	@PreAuthorize("hasRole('deployer')")
	@CacheEvict(value = "HitCache", key = "'cb-testcases'", allEntries = true)
	// @ApiOperation(value = "Adds Test Step or Update if already exists",
	// nickname = "addOrUpdateTS")
	@RequestMapping(value = "/cb/addOrUpdate/testStep", method = RequestMethod.POST, produces = "application/json")
	public List<ResourceUploadStatus> addTestStep(@ModelAttribute("requestObj") AddOrUpdateRequest request)
			throws IOException {
		List<ResourceUploadStatus> results = new ArrayList<>();

		resourceLoader.setDirectory(request.getZip() + "/");

		List<TestStep> steps = resourceLoader.createTS();

		for (TestStep ts : steps) {
			ResourceUploadStatus tmp = resourceLoader.handleTS(request.getId(), ts);
			results.add(tmp);
		}

		FileUtils.deleteDirectory(new File(request.getZip()));
		return results;
	}

	// Test Case

	@PreAuthorize("hasRole('deployer')")
	@CacheEvict(value = "HitCache", key = "'cb-testcases'", allEntries = true)
	// @ApiOperation(value = "Updates an existing Test Case", nickname =
	// "updateTC")
	@RequestMapping(value = "/cb/update/testCase", method = RequestMethod.POST, produces = "application/json")
	public List<ResourceUploadStatus> updateTestCase(@ModelAttribute("requestObj") AddOrUpdateRequest request)
			throws IOException {
		List<ResourceUploadStatus> results = new ArrayList<>();

		resourceLoader.setDirectory(request.getZip() + "/");

		List<TestCase> cases = resourceLoader.createTC();
		for (TestCase tc : cases) {
			ResourceUploadStatus tmp = resourceLoader.updateTC(tc);
			results.add(tmp);
		}

		FileUtils.deleteDirectory(new File(request.getZip()));
		return results;
	}

	@PreAuthorize("hasRole('deployer')")
	@CacheEvict(value = "HitCache", key = "'cb-testcases'", allEntries = true)
	// @ApiOperation(value = "Adds a Test Case to a Test Plan", nickname =
	// "addTCtoTP")
	@RequestMapping(value = "/cb/add/testCase/to/testPlan", method = RequestMethod.POST, produces = "application/json")
	public List<ResourceUploadStatus> addTestCaseToPlan(@ModelAttribute("requestObj") AddOrUpdateRequest request)
			throws IOException {
		List<ResourceUploadStatus> results = new ArrayList<>();

		resourceLoader.setDirectory(request.getZip() + "/");

		List<TestCase> cases = resourceLoader.createTC();
		for (TestCase tc : cases) {
			ResourceUploadStatus tmp = resourceLoader.addTC(request.getId(), tc, "plan");
			results.add(tmp);
		}

		FileUtils.deleteDirectory(new File(request.getZip()));
		return results;
	}

	@PreAuthorize("hasRole('deployer')")
	@CacheEvict(value = "HitCache", key = "'cb-testcases'", allEntries = true)
	// @ApiOperation(value = "Adds a Test Case to a Test Case Group", nickname =
	// "addTCtoTCG")
	@RequestMapping(value = "/cb/add/testCase/to/testCaseGroup", method = RequestMethod.POST, produces = "application/json")
	public List<ResourceUploadStatus> addTestCaseToGroup(@ModelAttribute("requestObj") AddOrUpdateRequest request)
			throws ProfileParserException, IOException, NotFoundException {

		List<ResourceUploadStatus> results = new ArrayList<>();
		resourceLoader.setDirectory(request.getZip() + "/");

		List<TestCase> cases = resourceLoader.createTC();
		for (TestCase tc : cases) {
			ResourceUploadStatus tmp = resourceLoader.addTC(request.getId(), tc, "group");
			results.add(tmp);
		}

		FileUtils.deleteDirectory(new File(request.getZip()));
		return results;
	}

	// Test Case Group

	@PreAuthorize("hasRole('deployer')")
	@CacheEvict(value = "HitCache", key = "'cb-testcases'", allEntries = true)
	// @ApiOperation(value = "Updates an existing Test Case Group", nickname =
	// "updateTCG")
	@RequestMapping(value = "/cb/update/testCaseGroup", method = RequestMethod.POST, produces = "application/json")
	public List<ResourceUploadStatus> updateTestGroup(@ModelAttribute("requestObj") AddOrUpdateRequest request)
			throws ProfileParserException, IOException, NotFoundException {

		List<ResourceUploadStatus> results = new ArrayList<>();
		resourceLoader.setDirectory(request.getZip() + "/");

		List<TestCaseGroup> groups = resourceLoader.createTCG();
		for (TestCaseGroup tcg : groups) {
			ResourceUploadStatus tmp = resourceLoader.updateTCG(tcg);
			results.add(tmp);
		}

		FileUtils.deleteDirectory(new File(request.getZip()));
		return results;
	}

	@PreAuthorize("hasRole('deployer')")
	@CacheEvict(value = "HitCache", key = "'cb-testcases'", allEntries = true)
	// @ApiOperation(value = "Adds a Test Case Group to a Test Plan", nickname =
	// "addTCGtoTP")
	@RequestMapping(value = "/cb/add/testCaseGroup/to/testPlan", method = RequestMethod.POST, produces = "application/json")
	public List<ResourceUploadStatus> addTestGroupToPlan(@ModelAttribute("requestObj") AddOrUpdateRequest request)
			throws IOException {

		List<ResourceUploadStatus> results = new ArrayList<>();
		resourceLoader.setDirectory(request.getZip() + "/");

		List<TestCaseGroup> groups = resourceLoader.createTCG();
		for (TestCaseGroup tcg : groups) {
			ResourceUploadStatus tmp = resourceLoader.addTCG(request.getId(), tcg, "plan");
			results.add(tmp);
		}

		FileUtils.deleteDirectory(new File(request.getZip()));
		return results;
	}

	@PreAuthorize("hasRole('deployer')")
	@CacheEvict(value = "HitCache", key = "'cb-testcases'", allEntries = true)
	// @ApiOperation(value = "Adds a Test Case Group to a Test Case Group",
	// nickname = "addTCGtoTCG")
	@RequestMapping(value = "/cb/add/testCaseGroup/to/testCaseGroup", method = RequestMethod.POST, produces = "application/json")
	public List<ResourceUploadStatus> addTestGroupToGroup(@ModelAttribute("requestObj") AddOrUpdateRequest request)
			throws IOException {

		List<ResourceUploadStatus> results = new ArrayList<>();
		resourceLoader.setDirectory(request.getZip() + "/");

		List<TestCaseGroup> groups = resourceLoader.createTCG();
		for (TestCaseGroup tcg : groups) {
			ResourceUploadStatus tmp = resourceLoader.addTCG(request.getId(), tcg, "group");
			results.add(tmp);
		}

		FileUtils.deleteDirectory(new File(request.getZip()));
		return results;
	}

	// Test Plan

	@PreAuthorize("hasRole('deployer')")
	@CacheEvict(value = "HitCache", key = "'cb-testcases'", allEntries = true)
	// @ApiOperation(value = "Adds Test Plan or Update if already exists",
	// nickname = "addOrUpdateTP")
	@RequestMapping(value = "/cb/addOrUpdate/testPlan", method = RequestMethod.POST, produces = "application/json")
	public List<ResourceUploadStatus> addOrUpdateTestPlan(@ModelAttribute("requestObj") AddOrUpdateRequest request)
			throws IOException {

		List<ResourceUploadStatus> results = new ArrayList<>();
		;
		resourceLoader.setDirectory(request.getZip() + "/");

		List<TestPlan> plans = resourceLoader.createTP();

		for (TestPlan tp : plans) {
			ResourceUploadStatus tmp = resourceLoader.handleTP(tp);
			results.add(tmp);
		}

		FileUtils.deleteDirectory(new File(request.getZip()));
		return results;
	}

	// Context Free API Methods

	@PreAuthorize("hasRole('deployer')")
	@CacheEvict(value = "HitCache", key = "'cf-testcases'", allEntries = true)
	// @ApiOperation(value = "Adds Context-Free Test Case or Update if already
	// exists", nickname = "cf_addOrUpdateTC")
	@RequestMapping(value = "/cf/addOrUpdate/testCase", method = RequestMethod.POST, produces = "application/json")
	public List<ResourceUploadStatus> addCFTestCase(@ModelAttribute("requestObj") AddOrUpdateRequest request)
			throws IOException {

		List<ResourceUploadStatus> results = new ArrayList<>();
		resourceLoader.setDirectory(request.getZip() + "/");

		List<CFTestStep> cases = resourceLoader.createCFTC();
		for (CFTestStep tc : cases) {
			ResourceUploadStatus tmp = resourceLoader.handleCFTC(request.getId(), tc);
			results.add(tmp);
		}

		FileUtils.deleteDirectory(new File(request.getZip()));
		return results;

	}

	// Global Resources API Methods

	@PreAuthorize("hasRole('deployer')")
	// @ApiOperation(value = "Adds Profile or Update if already exists",
	// nickname = "addOrUpdateProfile")
	@RequestMapping(value = "/global/addOrUpdate/profile", method = RequestMethod.POST, produces = "application/json")
	public List<ResourceUploadStatus> addOrUpdateProfile(@ModelAttribute("requestObj") AddOrUpdateRequest request)
			throws IOException {

		resourceLoader.setDirectory(request.getZip() + "/");

		List<ResourceUploadStatus> res = resourceLoader.addOrReplaceIntegrationProfile();

		FileUtils.deleteDirectory(new File(request.getZip()));
		return res;
	}

	@PreAuthorize("hasRole('deployer')")
	// @ApiOperation(value = "Adds Constraints or Update if already exists",
	// nickname = "addOrUpdateConstraints")
	@RequestMapping(value = "/global/addOrUpdate/constraints", method = RequestMethod.POST, produces = "application/json")
	public List<ResourceUploadStatus> addOrUpdateConstraints(@ModelAttribute("requestObj") AddOrUpdateRequest request)
			throws IOException {

		resourceLoader.setDirectory(request.getZip() + "/");

		List<ResourceUploadStatus> res = resourceLoader.addOrReplaceConstraints();

		FileUtils.deleteDirectory(new File(request.getZip()));
		return res;
	}

	@PreAuthorize("hasRole('deployer')")
	// @ApiOperation(value = "Adds ValueSet or Update if already exists",
	// nickname = "addOrUpdateValueSet")
	@RequestMapping(value = "/global/addOrUpdate/valueSet", method = RequestMethod.POST, produces = "application/json")
	public List<ResourceUploadStatus> addOrUpdateValueSet(@ModelAttribute("requestObj") AddOrUpdateRequest request)
			throws IOException {

		resourceLoader.setDirectory(request.getZip() + "/");

		List<ResourceUploadStatus> res = resourceLoader.addOrReplaceValueSet();

		FileUtils.deleteDirectory(new File(request.getZip()));
		return res;
	}

}
