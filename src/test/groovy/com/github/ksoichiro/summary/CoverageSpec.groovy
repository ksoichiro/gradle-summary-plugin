package com.github.ksoichiro.summary

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static com.github.ksoichiro.summary.Utils.createFile

class CoverageSpec extends Specification {
    static final String PLUGIN_ID = 'com.github.ksoichiro.summary'

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder()
    File rootDir
    File buildFile
    List<File> pluginClasspath

    void setup() {
        rootDir = testProjectDir.root
        buildFile = testProjectDir.newFile("build.gradle")
        def settingsFile = testProjectDir.newFile("settings.gradle")
        settingsFile << """
            include ':project1', ':project2'
            """

        def pluginClasspathResource = getClass().classLoader.findResource("plugin-classpath.txt")
        if (pluginClasspathResource == null) {
            throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
        }

        pluginClasspath = pluginClasspathResource.readLines()
            .collect { it.replace('\\', '\\\\') } // escape backslashes in Windows paths
            .collect { new File(it) }

        createFile(testProjectDir.root, "project1/src") {
            createFile(it, "main/java/com/example") {
                createFile(it, "A.java") << """
                    package com.example;
                    
                    public class A {
                        public String greet() {
                            System.out.println("debug log");
                            return "Hello";
                        }
                    }
                    """
                createFile(it, "B.java") << """
                    package com.example;
                    
                    public class B {
                        public String greet() {
                            return "Bye";
                        }
                    }
                    """
                createFile(it, "C.java") << """
                    package com.example;
                    
                    public class C {
                        public String greet() {
                            return "Good morning";
                        }
                    }
                    """
            }
            createFile(it, "test/java/com/example") {
                createFile(it, "ATest.java") << """
                    package com.example;
                    
                    import org.junit.Before;
                    import org.junit.Test;
                    
                    import static org.junit.Assert.*;
                    
                    public class ATest {
                        A instance;
                    
                        @Before
                        public void setup() {
                            instance = new A();
                        }
                    
                        @Test
                        public void greet() {
                            System.out.println("debug log in test2");
                            assertEquals("Hello", instance.greet());
                        }
                    }
                    """
                createFile(it, "BTest.java") << """
                    package com.example;
                    
                    import org.junit.Before;
                    import org.junit.Test;
                    
                    import static org.junit.Assert.*;
                    
                    public class BTest {
                        B instance;
                    
                        @Before
                        public void setup() {
                            instance = new B();
                        }
                    
                        @Test
                        public void greet() {
                            assertEquals("Bye", instance.greet());
                        }
                    }
                    """
            }
        }

        createFile(testProjectDir.root, "project2/src") {
            createFile(it, "main/java/com/example") {
                createFile(it, "D.java") << """
                    package com.example;
                    
                    public class D {
                        public String greet() {
                            System.out.print("This ");
                            System.out.print("class ");
                            System.out.print("really ");
                            System.out.print("does ");
                            System.out.print("nothing. ");
                            System.out.print("I ");
                            System.out.print("just ");
                            System.out.print("want ");
                            System.out.print("to ");
                            System.out.print("see ");
                            System.out.print("one ");
                            System.out.print("digit ");
                            System.out.print("coverage. ");
                            System.out.print("Is ");
                            System.out.print("it ");
                            System.out.print("correctly ");
                            System.out.print("aligned ");
                            System.out.print("to ");
                            System.out.print("the ");
                            System.out.println("right?");
                            return "Hello";
                        }
                    }
                    """
                createFile(it, "E.java") << """
                    package com.example;
                    
                    public class E {
                        public String greet() {
                            return "Bye";
                        }
                    }
                    """
                createFile(it, "F.java") << """
                    package com.example;
                    
                    public class F {
                        public String greet() {
                            return "Good morning";
                        }
                    }
                    """
            }
            createFile(it, "test/java/com/example") {
                createFile(it, "DTest.java") << """
                    package com.example;
                    
                    import org.junit.Before;
                    import org.junit.Test;
                    
                    import static org.junit.Assert.*;
                    
                    public class DTest {
                        D instance;
                    
                        @Before
                        public void setup() {
                            instance = new D();
                        }
                    
                        @Test
                        public void greet() {
                            System.out.println("To show low coverage, this test does nothing.");
                        }
                    }
                    """
            }
        }
    }
}
