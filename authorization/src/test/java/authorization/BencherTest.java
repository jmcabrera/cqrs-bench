package authorization;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ BenchCQRS.class, BenchJDBC.class })
public class BencherTest {
}
