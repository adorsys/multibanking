import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'cycle'
})
export class CyclePipe implements PipeTransform {

  transform(value: string): string {
    switch (value) {
      case 'WEEKLY':
        return 'weekly';
      case 'MONTHLY':
        return 'monthly';
      case 'TWO_MONTHLY':
        return 'bimonthly';
      case 'QUARTERLY':
        return 'quarterly';
      case 'HALFYEARLY':
        return 'half-yearly';
      case 'YEARLY':
        return 'yearly';
      default:
        return value;
    }
  }

}
