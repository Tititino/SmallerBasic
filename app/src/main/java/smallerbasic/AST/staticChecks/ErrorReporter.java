package smallerbasic.AST.staticChecks;

import org.jetbrains.annotations.NotNull;

public interface ErrorReporter {
    ErrorReporter STDERR_REPORTER = new ErrorReporter() {
        @Override
        public void reportError(@NotNull String msg) {
            System.err.println(msg);
        }
    };

    default void reportError(@NotNull String msg) {
        System.out.println(msg);
    }

}
