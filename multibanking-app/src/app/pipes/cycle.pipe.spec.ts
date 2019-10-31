import { CyclePipe } from './cycle.pipe';

describe('CyclePipe', () => {
  it('create an instance', () => {
    const pipe = new CyclePipe();
    expect(pipe).toBeTruthy();
  });
});
