package gov.nist.hit.core.domain;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class CFTestPlan extends AbstractTestCase implements Serializable {

  private static final long serialVersionUID = 880596750847898513L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  public CFTestPlan() {
    super();
    this.type = ObjectType.TestPlan;
    this.stage = TestingStage.CF;
  }

  public CFTestPlan(Long id, String name, String description, int position, Long persistentId) {
    super();
    this.id = id;
    this.name = name;
    this.description = description;
    this.position = position;
    this.persistentId = persistentId;
  }


  @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
  @JoinTable(name = "cf_tcg_tc",
      joinColumns = {@JoinColumn(name = "cf_tcg_id", referencedColumnName = "id")},
      inverseJoinColumns = {@JoinColumn(name = "cf_tc_id", referencedColumnName = "id")})
  @JsonProperty("children")
  private Set<CFTestStep> testCases = new HashSet<CFTestStep>();


  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  public Set<CFTestStep> getTestCases() {
    return testCases;
  }

  public void setTestCases(Set<CFTestStep> testCases) {
    this.testCases = testCases;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }



}
