import { IbanPipe } from './iban.pipe';

describe('IbanPipe', () => {
  it('create an instance', () => {
    const pipe = new IbanPipe();
    expect(pipe).toBeTruthy();
  });
});
