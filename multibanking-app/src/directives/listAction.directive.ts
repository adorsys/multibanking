import { Component, Input, ChangeDetectorRef } from '@angular/core';
import 'rxjs/Rx';

@Component({
    selector: 'list-action',
    template: '<ion-icon name="{{name}}"></ion-icon>',
    host: {
        '[class.action-enabled]': 'enabled',
        '[class.action-visible]': 'visible'
    }
})

export class ListActionDirective {
    
    enabled: boolean;
    visible: boolean;
    @Input() name: string;

    constructor(private cd: ChangeDetectorRef) {}

    @Input() set active(value: string) {
        if (value == 'true') {
            this.enabled = true;
            setTimeout(()=> {
                this.visible = true;
                this.cd.markForCheck();
            }, 100);
        } else if (value == 'false') {
            this.visible = false;
            setTimeout(()=> {
                this.enabled = false;
                this.cd.markForCheck();
            }, 100);
        }

    }
}