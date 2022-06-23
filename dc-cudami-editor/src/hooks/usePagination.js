import {useEffect, useState} from 'react'

import {findRootObjects} from '../api'

const usePagination = (apiContextPath, type, orders = []) => {
  const [content, setContent] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [numberOfPages, setNumberOfPages] = useState(0)
  const [pageNumber, setPageNumber] = useState(0)
  const [pageSize, setPageSize] = useState(20)
  const [searchTerm, setSearchTerm] = useState('')
  const [totalElements, setTotalElements] = useState(0)
  const loadData = async () => {
    const {content, totalElements} = await findRootObjects(
      apiContextPath,
      type,
      {pageNumber, pageSize, searchTerm, sorting: {orders}},
    )
    return {
      numberOfPages: Math.ceil(totalElements / pageSize),
      content,
      totalElements,
    }
  }
  useEffect(() => {
    setIsLoading(true)
    loadData()
      .then(({content, numberOfPages, totalElements}) => {
        setContent(content)
        setNumberOfPages(numberOfPages)
        setTotalElements(totalElements)
      })
      .finally(() => {
        setIsLoading(false)
      })
  }, [pageNumber, searchTerm])
  return {
    content,
    isLoading,
    numberOfPages,
    pageNumber,
    pageSize,
    searchTerm,
    totalElements,
    setContent,
    setPageNumber,
    setPageSize,
    setSearchTerm,
  }
}

export default usePagination
