import { TestBed } from '@angular/core/testing';

import { ApiarioService } from './apiario-service';

describe('ApiarioService', () => {
  let service: ApiarioService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ApiarioService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
