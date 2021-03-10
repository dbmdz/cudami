import classNames from 'classnames'
import React from 'react'
import {useTranslation} from 'react-i18next'
import ReactPaginate from 'react-paginate'

const Pagination = ({
  changePage,
  numberOfPages = 0,
  pageNumber = 0,
  position = 'above',
  showTotalElements = true,
  totalElements = 0,
  type,
}) => {
  const {t} = useTranslation()
  if (totalElements === 0) {
    return null
  }
  return (
    <div className="justify-content-start">
      <ReactPaginate
        activeClassName="active"
        breakClassName="page-item"
        breakLabel="&hellip;"
        breakLinkClassName="page-link"
        containerClassName={classNames('d-inline-flex pagination', {
          'mb-0': position === 'under',
          'mb-2': position === 'above',
          'mt-2': position === 'under',
        })}
        disabledClassName="disabled"
        forcePage={pageNumber}
        marginPagesDisplayed={1}
        nextClassName="page-item"
        nextLabel="&raquo;"
        nextLinkClassName="page-link"
        onPageChange={changePage}
        pageClassName="page-item"
        pageCount={numberOfPages}
        pageLinkClassName="page-link"
        pageRangeDisplayed={5}
        previousClassName="page-item"
        previousLabel="&laquo;"
        previousLinkClassName="page-link"
      />
      {showTotalElements > 0 && (
        <span className="ml-2">
          {t(`totalElements.${type}s`, {count: totalElements})}
        </span>
      )}
    </div>
  )
}

export default Pagination
