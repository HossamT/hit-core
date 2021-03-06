package gov.nist.hit.core.service;

import java.util.List;

import gov.nist.hit.core.domain.TestArtifact;
import gov.nist.hit.core.domain.TestPlan;
import gov.nist.hit.core.domain.TestScope;
import gov.nist.hit.core.domain.TestingStage;

public interface TestPlanService {

  TestPlan findOne(Long testPlanId);

  List<TestArtifact> findAllTestPackages(TestingStage stage);

  List<TestPlan> findShortAllByStage(TestingStage stage);

  List<TestPlan> findShortAllByStageAndScope(TestingStage stage, TestScope scope);

  List<TestPlan> findShortAllByStageAndAuthor(TestingStage stage, String authorUsername);

}
