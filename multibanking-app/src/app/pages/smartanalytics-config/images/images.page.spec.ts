import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ImagesPage } from './images.page';

describe('ImagesPage', () => {
  let component: ImagesPage;
  let fixture: ComponentFixture<ImagesPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ImagesPage ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ImagesPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
