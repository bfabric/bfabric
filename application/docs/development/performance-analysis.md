# B-Fabric — Performance Analysis

This page explains how to measure and log method execution times for server-side components using the project's lightweight timing utilities. The timing feature records how long annotated classes spend executing their methods and produces a readable call-time summary. Use it to find hotspots and expensive call paths.

## Required files

- `MeasureCalls.java` defines the `@MeasureCalls` annotation.
- `TimedInvocation.java` helper methods to record invocation times.
- `TimingInterceptor.java` collects and aggregates timings for invoked methods.
- `MeasureCallsFilter.java` web filter that enables per-request measurement and logging.

## How to enable measurement

1. Annotate the class you want to measure with `@MeasureCalls`:

    ```java
    @MeasureCalls
    public class UserManager extends AbstractEntityManager<User> {
      // ...
    }
    ```

2. Enable the web filter (example using `@WebFilter`):

    ```java
    @WebFilter(urlPatterns = { "*.html" })
    public class MeasureCallsFilter implements Filter {
      // filter setup that initializes timing for each request
    }
    ```

When the filter is active, each request to matching URLs will collect timing data for invoked methods in annotated classes and log a summary at the end of the request.

## Log format and interpretation

The log lists total time per top-level method, followed by indented child-method timings. Columns typically show:
- elapsed time (ms)
- call count
- fully qualified method name

Example output:

```
2821.036588 ms 8216 SampleBatchManager.isColumnSelected()
 35.343661 ms     16 SampleBatchManager.getSelectionList()
 26.034689 ms     78 SampleBatchManager.getCheckedSelectionListItems()
 2.454279 ms       5 SampleBatchManager.getSampleType()
 0.381102 ms       1 SampleBatchManager.getProjectId()
 0.37383 ms        1 SampleBatchManager.clearAllLists()
 0.373403 ms       1 SampleBatchManager.sampleTypeChanged()
```