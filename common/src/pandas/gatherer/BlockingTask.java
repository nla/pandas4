package pandas.gatherer;

import pandas.gather.Instance;

public record BlockingTask(String threadName, long instanceId, String titleName, String reason) {
    public BlockingTask(String threadName, Instance instance, String reason) {
        this(threadName, instance.getId(), instance.getTitle().getName(), reason);
    }
}