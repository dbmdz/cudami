import {useEffect, useState} from 'react'

import {loadRootIdentifiables} from '../api'

const usePagination = (apiContextPath, mockApi, type) => {
  const [content, setContent] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [numberOfPages, setNumberOfPages] = useState(0)
  const [pageNumber, setPageNumber] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const loadData = async (context, mock, pageNumber, pageSize = 20) => {
    const {content, totalElements} = await loadRootIdentifiables(
      context,
      mock,
      type,
      pageNumber,
      pageSize
    )
    return {
      numberOfPages: Math.ceil(totalElements / pageSize),
      content,
      totalElements,
    }
  }
  useEffect(() => {
    setIsLoading(true)
    loadData(apiContextPath, mockApi, pageNumber)
      .then(({content, numberOfPages, totalElements}) => {
        setNumberOfPages(numberOfPages)
        setContent(content)
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
    setPageNumber,
    totalElements,
  }
}

export default usePagination
