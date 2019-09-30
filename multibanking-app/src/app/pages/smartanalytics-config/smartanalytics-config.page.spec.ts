import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SmartanalyticsConfigPage } from './smartanalytics-config.page';

describe('SmartanalyticsConfigPage', () => {
  let component: SmartanalyticsConfigPage;
  let fixture: ComponentFixture<SmartanalyticsConfigPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SmartanalyticsConfigPage ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SmartanalyticsConfigPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
