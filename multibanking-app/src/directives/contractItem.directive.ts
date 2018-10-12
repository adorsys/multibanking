import { Component, Input } from '@angular/core';
import 'rxjs/Rx';
import { ContractEntity } from '../model/multibanking/models';
import { ImageService } from '../services/image.service';

@Component({
    selector: 'contract-item',
    templateUrl: 'contractItem.directive.html'
})

export class ContractItemDirective {

    getLogo: Function;

    @Input('contract') contract: ContractEntity;

    constructor(public logoService: ImageService) {
        this.getLogo = logoService.getImage;
    }
}