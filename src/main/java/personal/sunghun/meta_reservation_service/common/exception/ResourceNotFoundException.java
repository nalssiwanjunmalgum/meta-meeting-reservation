package personal.sunghun.meta_reservation_service.common.exception;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException() {
        super(ErrorCode.RESOURCE_NOT_FOUND);
    }
}
