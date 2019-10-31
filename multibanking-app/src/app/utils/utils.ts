import { ActivatedRouteSnapshot } from '@angular/router';

export function getHierarchicalRouteParam(route: ActivatedRouteSnapshot, paramName: string) {
  if (route.paramMap.get(paramName)) {
    return route.paramMap.get(paramName);
  }
  if (route.parent) {
    return getHierarchicalRouteParam(route.parent, paramName);
  }
  return null;
}
