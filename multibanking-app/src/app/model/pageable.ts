import { Link } from 'src/multibanking-api/link';

export interface Pageable {

    _embedded;
    _links: PageableLinks;
    page: Page;
}

export interface PageableLinks {

    first: Link;
    prev: Link;
    next: Link;
    last: Link;
}

export interface Page {

    number: number;
    size: number;
    totalElements: number;
    totalPages: number;
}
