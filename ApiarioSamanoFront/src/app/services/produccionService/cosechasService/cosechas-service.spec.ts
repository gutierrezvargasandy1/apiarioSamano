import { TestBed } from '@angular/core/testing';

import { CosechasService } from './cosechas-service';

describe('CosechasService', () => {
  let service: CosechasService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CosechasService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
