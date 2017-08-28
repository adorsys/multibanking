export interface Contract {
    "email"?: string;
    "homepage"?: string;
    "hotline"?: string;
    "interval"?: Contract.IntervalEnum;
    "logo"?: string;
    "mandateReference"?: string;
}

export namespace Contract {
    export enum IntervalEnum {
        WEEKLY = <any> 'WEEKLY',
        MONTHLY = <any> 'MONTHLY',
        QUARTERLY = <any> 'QUARTERLY',
        HALFYEARLY = <any> 'HALFYEARLY',
        YEARLY = <any> 'YEARLY'
    }
}
