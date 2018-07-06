import { Component, Input, EventEmitter, Output } from '@angular/core';

@Component({
    selector: 'resource-tree-view',
    template: `
    
    <div *ngFor="let item of TreeData" style="padding-left:20px">
         
        <ion-grid no-padding>
            <ion-row>
                <ion-col col-1 class="right" style="margin:auto;text-align:center">
                    <div>
                         <ion-icon item-start *ngIf="hasChildren(item)" (click)="toggleChildren(item)" name="add-circle" tappable></ion-icon>       
                     </div>
                 </ion-col>
        
                 <ion-col class="left">
                    <ion-item style="padding:0">
                        <ion-label style="font-size: 13px;font-weight: 500"> {{getItemName(item)}}</ion-label>
                            <ion-checkbox *ngIf="hasCheckbox" style="margin-right: 15px" [(ngModel)]="item.checked" color="randem">
                            </ion-checkbox>
                    </ion-item>
                </ion-col>
            </ion-row>
        </ion-grid>

    <resource-tree-view [TreeData]="getItemChildren(item)" *ngIf="item.visible" [hasCheckbox]="hasCheckbox">
    </resource-tree-view>
    
    </div>
    `
})
export class ResourceTreeDirective {
    @Input() TreeData: any[];
    @Input() hasCheckbox: boolean = false;
    @Output() selected = new EventEmitter();

    constructor() { }

    toggleChildren(node: any) {
        node.visible = !node.visible;
    }

    getItemName(item: any) {
        return item.name;
    }

    getItemChildren(item: any) {
        if (item.subcategories) {
            return item.subcategories;
        } else if (item.specifications) {
            return item.specifications;
        }
        return [];
    }

    hasChildren(item: any) {
        return item.subcategories || item.specifications;
    }

    isVisible(item: any) {
        return true;
    }

}