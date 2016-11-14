package gov.nist.hit.core.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import gov.nist.hit.core.domain.CFTestInstance;
import gov.nist.hit.core.domain.TestCase;
import gov.nist.hit.core.domain.TestCaseGroup;
import gov.nist.hit.core.domain.TestPlan;
import gov.nist.hit.core.domain.TestStep;
import gov.nist.hit.core.domain.TestingStage;
import gov.nist.hit.core.repo.TestCaseGroupRepository;
import gov.nist.hit.core.repo.TestCaseRepository;
import gov.nist.hit.core.repo.TestStepRepository;
import gov.nist.hit.core.service.exception.NotFoundException;
import gov.nist.hit.core.service.util.ResourcebundleHelper;

public abstract class ResourceLoader extends ResourcebundleLoader {

  @Autowired
  private TestCaseRepository testCaseRepository;

  @Autowired
  private TestCaseGroupRepository testCaseGroupRepository;

  @Autowired
  private TestStepRepository testStepRepository;

  @Autowired
  @PersistenceContext(unitName = "base-tool")
  protected EntityManager entityManager;

  private String directory;

  public void setDirectory(String dir) {
    this.directory = dir;
  }

  public String getDirectory() {
    return this.directory;
  }

  public List<Resource> getApiDirectories(String pattern) throws IOException {
    // System.out.println("GET DIRS " + directory + pattern);
    List<Resource> res = ResourcebundleHelper.getDirectoriesFile(directory + pattern);
    // System.out.println(res.size());
    return res;
  }

  public Resource getApiResource(String pattern) throws IOException {
    // System.out.println("GET RES " + directory + pattern);
    return ResourcebundleHelper.getResourceFile(directory + pattern);
  }

  public List<Resource> getApiResources(String pattern) throws IOException {
    // System.out.println("GET RESS " + directory + pattern);
    return ResourcebundleHelper.getResourcesFile(directory + pattern);
  }



  // ------ Context-Free test Case

  public void handleCFTC(Long testCaseId, CFTestInstance tc) throws NotFoundException {

    CFTestInstance existing = this.testInstanceRepository.getByPersistentId(tc.getPersistentId());

    if (existing != null) {
      Long exId = existing.getId();
      tc.setId(exId);
      List<CFTestInstance> merged = this.mergeCFTC(tc.getChildren(), existing.getChildren());
      tc.setChildren(merged);
      this.testInstanceRepository.saveAndFlush(tc);
    } else {
      if (testCaseId != null && testCaseId != -1) {
        CFTestInstance parent = this.testInstanceRepository.getByPersistentId(testCaseId);
        if (parent == null)
          throw new NotFoundException();
        parent.getChildren().add(tc);
        this.testInstanceRepository.saveAndFlush(parent);
      } else {
        tc.setRoot(true);
        this.testInstanceRepository.saveAndFlush(tc);
      }
    }

  }

  // ------ Context-Based test Case

  public void handleTS(Long testCaseId, TestStep ts) throws NotFoundException {

    TestStep existing = this.testStepRepository.getByPersistentId(ts.getPersistentId());

    if (existing != null) {
      Long exId = existing.getId();
      ts.setId(exId);
      ts.setTestCase(existing.getTestCase());
      this.testStepRepository.saveAndFlush(ts);
    } else {
      TestCase tc = this.testCaseRepository.getByPersistentId(testCaseId);
      if (tc == null)
        throw new NotFoundException();
      tc.addTestStep(ts);
      this.testCaseRepository.saveAndFlush(tc);
    }

  }

  public void addTC(Long parentId, TestCase tc, String where) throws NotFoundException {

    if (where.toLowerCase().equals("group")) {
      TestCaseGroup tcg = this.testCaseGroupRepository.getByPersistentId(parentId);
      if (tcg == null)
        throw new NotFoundException();
      tcg.getTestCases().add(tc);
      this.testCaseGroupRepository.saveAndFlush(tcg);
    } else if (where.toLowerCase().equals("plan")) {
      TestPlan tp = this.testPlanRepository.getByPersistentId(parentId);
      if (tp == null)
        throw new NotFoundException();
      tp.getTestCases().add(tc);
      this.testPlanRepository.saveAndFlush(tp);
    }

  }

  public void updateTC(TestCase tc) throws NotFoundException {

    TestCase existing = this.testCaseRepository.getByPersistentId(tc.getPersistentId());

    if (existing != null) {
      Long exId = existing.getId();
      List<TestStep> merged = this.mergeTS(tc.getTestSteps(), existing.getTestSteps());
      tc.setId(exId);
      tc.setDataMappings(existing.getDataMappings());
      tc.setTestSteps(merged);
      this.testCaseRepository.saveAndFlush(tc);
    } else {
      throw new NotFoundException();
    }

  }

  public void addTCG(Long parentId, TestCaseGroup tcg, String where) throws NotFoundException {
    if (where.toLowerCase().equals("plan")) {
      TestPlan tp = this.testPlanRepository.getByPersistentId(parentId);
      if (tp == null)
        throw new NotFoundException();
      tp.getTestCaseGroups().add(tcg);
      this.testPlanRepository.saveAndFlush(tp);
    } else if (where.toLowerCase().equals("group")) {
      TestCaseGroup tcgg = this.testCaseGroupRepository.getByPersistentId(parentId);
      if (tcgg == null)
        throw new NotFoundException();
      tcgg.getTestCaseGroups().add(tcg);
      this.testCaseGroupRepository.saveAndFlush(tcgg);
    }
  }

  public void updateTCG(TestCaseGroup tcg) throws NotFoundException {
    TestCaseGroup existing = this.testCaseGroupRepository.getByPersistentId(tcg.getPersistentId());

    if (existing != null) {
      Long exId = existing.getId();
      List<TestCase> mergedTc = this.mergeTC(tcg.getTestCases(), existing.getTestCases());
      List<TestCaseGroup> mergedTcg =
          this.mergeTCG(tcg.getTestCaseGroups(), existing.getTestCaseGroups());
      tcg.setId(exId);
      tcg.setTestCases(mergedTc);
      tcg.setTestCaseGroups(mergedTcg);
      this.testCaseGroupRepository.saveAndFlush(tcg);
    } else {

      throw new NotFoundException();

    }
  }

  public void handleTP(TestPlan tp) {

    TestPlan existing = this.testPlanRepository.getByPersistentId(tp.getPersistentId());

    if (existing != null) {
      Long exId = existing.getId();
      List<TestCase> mergedTc = this.mergeTC(tp.getTestCases(), existing.getTestCases());
      List<TestCaseGroup> mergedTcg =
          this.mergeTCG(tp.getTestCaseGroups(), existing.getTestCaseGroups());
      tp.setId(exId);
      tp.setTestCases(mergedTc);
      tp.setTestCaseGroups(mergedTcg);
    }

    this.testPlanRepository.saveAndFlush(tp);
  }

  // ---- Helper Functions
  // Creation Methods

  public List<TestStep> createTS() throws IOException {
    System.out.println("Creating TS");
    List<TestStep> tmp = new ArrayList<TestStep>();
    List<Resource> resources = getApiDirectories("*");
    System.out.println("Directories found : " + resources.size());
    for (Resource resource : resources) {
      String fileName = resource.getFilename();
      System.out.println("Handling folder = " + fileName);
      TestStep testStep = testStep(fileName + "/", null, false);
      if (testStep != null) {
        tmp.add(testStep);
      }
    }
    return tmp;
  }

  public List<TestCase> createTC() throws IOException {
    List<TestCase> tmp = new ArrayList<TestCase>();
    List<Resource> resources = getApiDirectories("*");
    for (Resource resource : resources) {
      String fileName = resource.getFilename();
      TestCase testCase = testCase(fileName + "/", null, false);
      if (testCase != null) {
        tmp.add(testCase);
      }
    }
    return tmp;
  }

  public List<TestCaseGroup> createTCG() throws IOException {
    List<TestCaseGroup> tmp = new ArrayList<TestCaseGroup>();
    List<Resource> resources = getApiDirectories("*");
    for (Resource resource : resources) {
      String fileName = resource.getFilename();
      TestCaseGroup testCaseGroup = testCaseGroup(fileName + "/", null, false);
      if (testCaseGroup != null) {
        tmp.add(testCaseGroup);
      }
    }
    return tmp;
  }

  public List<TestPlan> createTP() throws IOException {
    List<TestPlan> tmp = new ArrayList<TestPlan>();
    List<Resource> resources = getApiDirectories("*");
    for (Resource resource : resources) {
      String fileName = resource.getFilename();
      TestPlan testPlan = testPlan(fileName + "/", TestingStage.CB);
      if (testPlan != null) {
        tmp.add(testPlan);
      }
    }
    return tmp;
  }

  public List<CFTestInstance> createCFTC() throws IOException {

    List<CFTestInstance> tmp = new ArrayList<CFTestInstance>();
    List<Resource> resources = getApiDirectories("*");
    for (Resource resource : resources) {
      String fileName = resource.getFilename();
      CFTestInstance testObject = testObject(fileName + "/");
      if (testObject != null) {
        tmp.add(testObject);
      }
    }
    return tmp;
  }

  // Merge Methods

  public List<TestStep> mergeTS(List<TestStep> newL, List<TestStep> oldL) {
    int index = -1;
    List<TestStep> tmp = new ArrayList<TestStep>();
    tmp.addAll(oldL);

    for (TestStep tcs : newL) {

      if ((index = tmp.indexOf(tcs)) != -1) {
        tcs.setId(tmp.get(index).getId());
        tmp.set(index, tcs);
      } else
        tmp.add(tcs);
    }

    return tmp;
  }

  public List<TestCase> mergeTC(List<TestCase> newL, List<TestCase> oldL) {
    int index = -1;
    List<TestCase> tmp = new ArrayList<TestCase>();
    tmp.addAll(oldL);

    for (TestCase tcs : newL) {

      if ((index = tmp.indexOf(tcs)) != -1) {
        List<TestStep> newLs = mergeTS(tcs.getTestSteps(), tmp.get(index).getTestSteps());
        tcs.setTestSteps(newLs);
        TestCase existing = tmp.get(index);
        tcs.setDataMappings(existing.getDataMappings());
        tcs.setId(existing.getId());
        tmp.set(index, tcs);
      } else
        tmp.add(tcs);
    }
    return tmp;
  }

  public List<TestCaseGroup> mergeTCG(List<TestCaseGroup> newL, List<TestCaseGroup> oldL) {
    int index = -1;
    List<TestCaseGroup> tmp = new ArrayList<TestCaseGroup>();
    tmp.addAll(oldL);

    for (TestCaseGroup tcs : newL) {

      if ((index = tmp.indexOf(tcs)) != -1) {
        List<TestCase> newLs = mergeTC(tcs.getTestCases(), tmp.get(index).getTestCases());
        tcs.setTestCases(newLs);
        if (tcs.getTestCaseGroups() != null && tcs.getTestCaseGroups().size() > 0) {
          List<TestCaseGroup> newLsg =
              mergeTCG(tcs.getTestCaseGroups(), tmp.get(index).getTestCaseGroups());
          tcs.setTestCaseGroups(newLsg);
        }
        tcs.setId(tmp.get(index).getId());
        tmp.set(index, tcs);
      } else
        tmp.add(tcs);
    }
    return tmp;
  }

  public List<CFTestInstance> mergeCFTC(List<CFTestInstance> newL, List<CFTestInstance> oldL) {
    int index = -1;
    List<CFTestInstance> tmp = new ArrayList<CFTestInstance>();
    tmp.addAll(oldL);

    for (CFTestInstance tcs : newL) {

      if ((index = tmp.indexOf(tcs)) != -1) {
        CFTestInstance existing = tmp.get(index);
        if (existing.getChildren() != null && existing.getChildren().size() > 0) {
          if (tcs.getChildren() != null && tcs.getChildren().size() > 0) {
            List<CFTestInstance> children = mergeCFTC(tcs.getChildren(), existing.getChildren());
            tcs.setChildren(children);
          } else {
            tcs.setChildren(existing.getChildren());
          }
        }
        tcs.setId(tmp.get(index).getId());
        tmp.set(index, tcs);
      } else
        tmp.add(tcs);
    }
    return tmp;
  }

  // Delete
  public void deleteTS(Long id) throws NotFoundException {
    TestStep s = this.testStepRepository.getByPersistentId(id);
    if (s == null)
      throw new NotFoundException();
    this.testStepRepository.delete(s.getId());

  }

  public void deleteTC(Long id) throws NotFoundException {
    TestCase s = this.testCaseRepository.getByPersistentId(id);
    if (s == null)
      throw new NotFoundException();
    this.testCaseRepository.delete(s.getId());
  }

  public void deleteTCG(Long id) throws NotFoundException {
    TestCaseGroup s = this.testCaseGroupRepository.getByPersistentId(id);
    if (s == null)
      throw new NotFoundException();
    this.testCaseGroupRepository.delete(s.getId());
  }

  public void deleteTP(Long id) throws NotFoundException {
    TestPlan s = this.testPlanRepository.getByPersistentId(id);
    if (s == null)
      throw new NotFoundException();
    this.testPlanRepository.delete(s.getId());
  }

  public void deleteCFTC(Long id) throws NotFoundException {
    CFTestInstance s = this.testInstanceRepository.getByPersistentId(id);
    if (s == null)
      throw new NotFoundException();
    this.testInstanceRepository.delete(s.getId());
  }


  public void flush() {
    this.testStepRepository.flush();
    this.testCaseRepository.flush();
    this.testCaseGroupRepository.flush();
    this.testPlanRepository.flush();
  }


  public abstract void addOrReplaceValueSet() throws IOException;

  public abstract void addOrReplaceConstraints() throws IOException;

  public abstract void addOrReplaceIntegrationProfile() throws IOException;



}