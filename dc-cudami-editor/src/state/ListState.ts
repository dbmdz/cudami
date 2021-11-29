export interface ListState {
  changeOfOrderActive: boolean
  identifiables: unknown[]
  identifierTypes: unknown[]
  isLoading: boolean
  numberOfPages: number
  pageNumber: number
  searchTerm: string
  showSearch: boolean
  totalElements: number
}

export const initialListState: ListState = {
  changeOfOrderActive: false,
  identifiables: [],
  identifierTypes: [],
  isLoading: false,
  numberOfPages: 0,
  pageNumber: 0,
  searchTerm: '',
  showSearch: false,
  totalElements: 0,
}
