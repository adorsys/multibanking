import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CustomRulesPage } from './custom-rules.page';

describe('CustomRulesPage', () => {
  let component: CustomRulesPage;
  let fixture: ComponentFixture<CustomRulesPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CustomRulesPage ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CustomRulesPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
