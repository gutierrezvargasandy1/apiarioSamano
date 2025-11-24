import { TestBed } from '@angular/core/testing';

import { LotesService } from './lotes-service';

describe('LotesService', () => {
  let service: LotesService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LotesService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
