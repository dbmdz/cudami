import {useEffect, useState} from 'react'

import {loadRootIdentifiables} from '../api'

const usePagination = (apiContextPath, type) => {
  const [content, setContent] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [numberOfPages, setNumberOfPages] = useState(0)
  const [pageNumber, setPageNumber] = useState(0)
  const [pageSize, setPageSize] = useState(20)
  const [totalElements, setTotalElements] = useState(0)
  const loadData = async (context, pageNumber, pageSize) => {
    const {content, totalElements} = await loadRootIdentifiables(
      context,
      type,
      {pageNumber, pageSize},
    )
    return {
      numberOfPages: Math.ceil(totalElements / pageSize),
      content,
      totalElements,
    }
  }
  useEffect(() => {
    setIsLoading(true)
    loadData(apiContextPath, pageNumber, pageSize)
      .then(({content, numberOfPages, totalElements}) => {
        setContent(content)
        setNumberOfPages(numberOfPages)
        setTotalElements(totalElements)
      })
      .finally(() => {
        setIsLoading(false)
      })
  }, [pageNumber])
  return {
    content,
    isLoading,
    numberOfPages,
    pageNumber,
    pageSize,
    totalElements,
    setContent,
    setPageNumber,
    setPageSize,
  }
}

export default usePagination
