import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BanksConfigPage } from './banks-config.page';

describe('BanksConfigPage', () => {
  let component: BanksConfigPage;
  let fixture: ComponentFixture<BanksConfigPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BanksConfigPage ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BanksConfigPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
