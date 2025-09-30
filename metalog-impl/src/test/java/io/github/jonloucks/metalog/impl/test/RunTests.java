package io.github.jonloucks.metalog.impl.test;

import io.github.jonloucks.metalog.test.Tools;
import io.github.jonloucks.metalog.test.Tests;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@ExtendWith(RunTests.RunExtension.class)
public final class RunTests implements Tests {

    public static final class RunExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
        public RunExtension() {

        }

        @Override
        public void afterTestExecution(ExtensionContext extensionContext) {
            Tools.clean();
        }
        
        @Override
        public void beforeTestExecution(ExtensionContext extensionContext) {
            Tools.clean();
        }
    }
}
