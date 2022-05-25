import {
  FaAngleUp,
  FaAsterisk,
  FaBold,
  FaCode,
  FaEdit,
  FaFilm,
  FaHeading,
  FaImage,
  FaItalic,
  FaLink,
  FaListOl,
  FaListUl,
  FaMinus,
  FaOutdent,
  FaParagraph,
  FaQuoteLeft,
  FaRedo,
  FaStrikethrough,
  FaSubscript,
  FaSuperscript,
  FaTable,
  FaTrashAlt,
  FaUnderline,
  FaUndo,
  FaWindowMaximize,
} from 'react-icons/fa'
import {
  RiDeleteColumn,
  RiDeleteRow,
  RiInsertColumnLeft,
  RiInsertColumnRight,
  RiInsertRowBottom,
  RiInsertRowTop,
} from 'react-icons/ri'

export default {
  addColumnAfter: <RiInsertColumnRight size="1.2em" />,
  addColumnBefore: <RiInsertColumnLeft size="1.2em" />,
  addRowAfter: <RiInsertRowBottom size="1.2em" />,
  addRowBefore: <RiInsertRowTop size="1.2em" />,
  blockquote: <FaQuoteLeft />,
  bold: <FaBold />,
  bullet_list: <FaListUl />,
  code_block: <FaCode />,
  code: <FaCode />,
  deleteColumn: <RiDeleteColumn size="1.2em" />,
  deleteRow: <RiDeleteRow size="1.2em" />,
  deleteTable: (
    <>
      <FaTable />
      <FaTrashAlt size="0.6em" style={{verticalAlign: 'bottom'}} />
    </>
  ),
  edit: <FaEdit />,
  em: <FaItalic />,
  footnote: <FaAsterisk />,
  heading: <FaHeading />,
  hr: <FaMinus />,
  iframe: <FaWindowMaximize />,
  image: <FaImage />,
  italic: <FaItalic />,
  join_up: <FaAngleUp />,
  lift: <FaOutdent />,
  link: <FaLink />,
  ordered_list: <FaListOl />,
  paragraph: <FaParagraph />,
  redo: <FaRedo />,
  strikethrough: <FaStrikethrough />,
  strong: <FaBold />,
  subscript: <FaSubscript />,
  superscript: <FaSuperscript />,
  table: <FaTable />,
  underline: <FaUnderline />,
  undo: <FaUndo />,
  video: <FaFilm />,
}
