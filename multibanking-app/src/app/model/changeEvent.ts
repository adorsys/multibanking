export interface ChangeEvent<T> {

    data: T;
    eventType: EventType;

}
export enum EventType {
    Create,
    Update,
    Delete,
}


