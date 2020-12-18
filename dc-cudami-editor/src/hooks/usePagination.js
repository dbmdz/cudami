import {useEffect, useState} from 'react'

import {loadIdentifiables} from '../api'

const usePagination = (apiContextPath, mockApi, type) => {
  const [content, setContent] = useState([])
  const [numberOfPages, setNumberOfPages] = useState(0)
  const [pageNumber, setPageNumber] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const loadData = async (context, mock, pageNumber, pageSize = 20) => {
    const {content, totalElements} = await loadIdentifiables(
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
    loadData(apiContextPath, mockApi, pageNumber).then(
      ({content, numberOfPages, totalElements}) => {
        setNumberOfPages(numberOfPages)
        setContent(content)
        setTotalElements(totalElements)
      }
    )
  }, [pageNumber])
  return {
    content,
    numberOfPages,
    pageNumber,
    setPageNumber,
    totalElements,
  }
}

export default usePagination
