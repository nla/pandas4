package pandas.collection;

/**
 * Published when a title is transferred to a new owner via {@link TitleService#transferOwnership}.
 */
public record TitleTransferredEvent(OwnerHistory ownerHistory) {
}
