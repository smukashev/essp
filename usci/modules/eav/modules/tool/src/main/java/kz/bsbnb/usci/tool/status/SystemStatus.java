package kz.bsbnb.usci.tool.status;

import java.io.Serializable;

public class SystemStatus implements Serializable {
    private ReceiverStatus receiverStatus;

    private SyncStatus syncStatus;

    private CoreStatus coreStatus;

    public SystemStatus(ReceiverStatus receiverStatus, SyncStatus syncStatus, CoreStatus coreStatus) {
        this.receiverStatus = receiverStatus;
        this.syncStatus = syncStatus;
        this.coreStatus = coreStatus;
    }

    public ReceiverStatus getReceiverStatus() {
        return receiverStatus;
    }

    public void setReceiverStatus(ReceiverStatus receiverStatus) {
        this.receiverStatus = receiverStatus;
    }

    public SyncStatus getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(SyncStatus syncStatus) {
        this.syncStatus = syncStatus;
    }

    public CoreStatus getCoreStatus() {
        return coreStatus;
    }

    public void setCoreStatus(CoreStatus coreStatus) {
        this.coreStatus = coreStatus;
    }

    @Override
    public String toString() {
        return "SystemStatus{" +
                "receiverStatus=" + receiverStatus +
                ", syncStatus=" + syncStatus +
                ", coreStatus=" + coreStatus +
                '}';
    }
}
