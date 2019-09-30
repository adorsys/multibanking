import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { StaticRulesPage } from './static-rules.page';

describe('StaticRulesPage', () => {
  let component: StaticRulesPage;
  let fixture: ComponentFixture<StaticRulesPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ StaticRulesPage ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(StaticRulesPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
