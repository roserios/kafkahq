package org.kafkahq.modules;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.kafkahq.configs.Connection;
import org.kafkahq.controllers.AbstractController;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Singleton
@Slf4j
public class RequestHelper {
    @Value("${kafkahq.server.base-path}")
    protected String basePath;

    @Inject
    private List<Connection> connections;

    public Optional<String> getClusterId(HttpRequest<?> request) {
        String path = request.getPath();
        if (!basePath.equals("") && path.indexOf(basePath) == 0) {
            path = path.substring(basePath.length());
        }

        List<String> pathSplit = Arrays.asList(path.split("/"));

        if (pathSplit.size() >= 2) {
            String clusterId = pathSplit.get(1);

            return connections
                .stream()
                .filter(connection -> connection.getName().equals(clusterId))
                .map(r -> clusterId)
                .findFirst();
        }

        return Optional.empty();
    }

    public static AbstractController.Toast runnableToToast(ResultStatusResponseRunnable callable, String successMessage, String failedMessage) {
        AbstractController.Toast.ToastBuilder builder = AbstractController.Toast.builder();

        try {
            callable.run();
            builder
                .message(successMessage)
                .type(AbstractController.Toast.Type.success);
        } catch (Exception exception) {
            String cause = exception.getCause() != null ? exception.getCause().getMessage() : exception.getMessage();

            builder
                .title(failedMessage)
                .message(exception.getCause() != null ? exception.getCause().getMessage() : exception.getMessage())
                .type(AbstractController.Toast.Type.error);

            log.error(failedMessage != null ? failedMessage : cause, exception);
        }

        return builder.build();
    }

    public interface ResultStatusResponseRunnable {
        void run() throws Exception;
    }
}
