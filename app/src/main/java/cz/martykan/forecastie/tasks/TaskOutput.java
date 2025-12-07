package cz.martykan.forecastie.tasks;

public class TaskOutput {
    // Indicates result of parsing server response
    ParseResult parseResult;
    // Indicates result of background task
    TaskResult taskResult;
    // Error caused unsuccessful result
    Throwable taskError;
    // Response data from the request
    String response;
}
