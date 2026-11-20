Foreword

The goal of the seminar is to understand how the main kinds of tests are used and to try implementing them.

About the assignment

    Before you start, you need to study some topics on your own. Look for useful links in the description.
    At first glance the assignment may seem large, but there will actually not be that much work with code.

Context

After three seminars you have a satellite constellation management system that consists of:

1. Base entities: Satellite, CommunicationSatellite, ImagingSatellite, SatelliteState, EnergySystem
2. Service layer: SpaceOperationCenterService
3. Repository: ConstellationRepository
4. Domain model: SatelliteConstellation

In the lecture we discussed the role of testing. While your code is not too large yet, it is the right time to lay a common foundation for tests.
Preparing the project

In the project we will use three kinds of tests. For each of them create a class (for each implementation in the corresponding seminars package):

     ConstellationRepositoryUnitTest — unit tests
     ConstellationRepositoryMockTest — mock tests
     ConstellationRepositoryIntegrationTest — integration tests

And, of course, let's add a few dependencies:

1. The test starter

This is a set of Spring Boot libraries that you add once to get the standard testing stack.

2. The JaCoCo plugin, which generates the test coverage report

    Gradle is the build system that also downloads dependencies. We have already used the build.gradle.kts file, where tasks can be configured. Gradle links tasks so that jacocoTestReport runs automatically after test:

  tasks.test {
    finalizedBy(tasks.jacocoTestReport)
  }
  
  tasks.jacocoTestReport {
    dependsOn(tasks.test)
  }


3. The lombok code generation library

    lombok is a widely used Java library that generates code from annotations. This means that getters, constructors, toString and other basic class methods no longer have to be written by hand.

Since we are adding lombok, rewrite the project to use it as much as possible. For small pet projects and some production solutions this library is very convenient.
Unit tests

Write unit tests for ConstellationRepository that check every method of the class. Note that in this setup the class must not know about external dependencies and is created with the new operator.
Requirements for unit tests

1. Use the right annotations

     @DisplayName — to describe every test method
    @Test — on test methods

2. Keep shared constants for test data (names, values)
3. Test coverage must be at least 60% for non-domain classes
4. Take into account:

    positive and negative scenarios;
    edge cases — for example, entering maximum values.

5. Method names, variable names and @DisplayName descriptions must be easy for other developers to understand

    Do not put satellites into variables named a and b

6. Use assertEquals, assertTrue and other methods from org.junit.jupiter.api.Assertions

7. One test class covers one real class

8. One test method covers only one business scenario
Mock tests

Modify the tests for ConstellationRepository to use mocks.
All requirements for unit tests also apply to mock tests.
 
Other requirements for mock tests


1. Use the right annotations

    @ExtendWith(MockitoExtension.class) — on the whole mock test class
    @Mock — on the class field (the repository) to inject a mock object

2. Create in advance the objects that check the final result or the business scenario
3. Pass the created objects to the mock calls: when(repository.getAllConstellations()).thenReturn(constellations)
3. Use verify() to check calls of the internal function
Integration tests


Write integration tests that check how components interact inside a running Spring Boot context. In particular, rework the previous test class so that:

    the repository is injected through DI;
    the full lifecycle of the objects that pass through it can be checked.

The requirements for mock tests do not apply here, but the requirements for unit tests still do.

Other requirements for integration tests

1. Use the right annotations:

    @SpringBootTest — on the whole integration test class
    @Autowired — to inject the class we need automatically (use it instead of @Mock)


2. Implement the full object lifecycle: create a constellation → add satellites → activate → execute missions
3. Check the state of the objects between operations with assertions
Running and checking


For testing in the Java ecosystem, Gradle has useful commands that most real projects need:

1. Run the tests: ./gradlew test (make sure all tests pass)
2. View the test results report: look in build/reports/tests/test/index.html
2. Run with the JaCoCo report: ./gradlew test jacocoTestReport
3. View the coverage report: look in build/reports/jacoco/test/html/index.html
Extra, for self-study

Try to write tests for SpaceOperationCenterService on your own.

Writing unit tests will not be that interesting, so focus on mocks and integration testing. This will help you understand how injection inside tests should really be set up.
