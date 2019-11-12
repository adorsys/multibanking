import { Component, Input } from '@angular/core';

import { ImagesService } from '../../services/rest/images.service';
import { ContractTO } from './../../../multibanking-api/contractTO';

@Component({
  selector: 'app-contract-item',
  templateUrl: './contract-item.component.html',
  styleUrls: ['./contract-item.component.scss'],
})
export class ContractItemComponent {

  @Input() contract: ContractTO;

  constructor(private imagesService: ImagesService) {}

  getLogo(image: string): string {
    return this.imagesService.getImage(image);
  }
}
