import classNames from 'classnames'
import {useContext} from 'react'
import {useTranslation} from 'react-i18next'
import ReactPaginate from 'react-paginate'

import AppContext from './AppContext'

const Pagination = ({
  changePage,
  numberOfPages = 0,
  pageNumber = 0,
  position = 'above',
  showTotalElements = true,
  totalElements = 0,
  type,
}) => {
  const {uiLocale} = useContext(AppContext)
  const {t} = useTranslation()
  if (totalElements === 0) {
    return <div />
  }
  return (
    <div>
      <ReactPaginate
        activeClassName="active"
        breakClassName="page-item"
        breakLabel="&hellip;"
        breakLinkClassName="page-link"
        containerClassName={classNames(
          'd-inline-flex',
          'pagination',
          position === 'under' && ['mb-0', 'mt-2'],
          {
            'mb-2': position === 'above',
          },
        )}
        disabledClassName="disabled"
        forcePage={pageNumber}
        marginPagesDisplayed={1}
        nextClassName="page-item"
        nextLabel="&raquo;"
        nextLinkClassName="page-link"
        onPageChange={changePage}
        pageClassName="page-item"
        pageCount={numberOfPages}
        pageLabelBuilder={(page) => page.toLocaleString(uiLocale)}
        pageLinkClassName="page-link"
        pageRangeDisplayed={5}
        previousClassName="page-item"
        previousLabel="&laquo;"
        previousLinkClassName="page-link"
      />
      {showTotalElements && (
        <span className="ml-2">
          {t(`pagination:totalElements.${type}`, {
            count: totalElements,
            formattedCount: totalElements.toLocaleString(uiLocale),
          })}
        </span>
      )}
    </div>
  )
}

export default Pagination
