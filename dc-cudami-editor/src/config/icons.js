import React from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faAngleUp,
  faAsterisk,
  faBold,
  faCode,
  faHeading,
  faImage,
  faItalic,
  faLink,
  faListOl,
  faListUl,
  faMinus,
  faOutdent,
  faParagraph,
  faQuoteLeft,
  faRedo,
  faStrikethrough,
  faSubscript,
  faSuperscript,
  faTable,
  faUnderline,
  faUndo
} from '@fortawesome/free-solid-svg-icons';

export default {
  blockquote: <FontAwesomeIcon icon={faQuoteLeft} />,
  bold: <FontAwesomeIcon icon={faBold} />,
  bullet_list: <FontAwesomeIcon icon={faListUl} />,
  code_block: <FontAwesomeIcon icon={faCode} />,
  code: <FontAwesomeIcon icon={faCode} />,
  em: <FontAwesomeIcon icon={faItalic} />,
  footnote: <FontAwesomeIcon icon={faAsterisk} />,
  heading: <FontAwesomeIcon icon={faHeading} />,
  hr: <FontAwesomeIcon icon={faMinus} />,
  image: <FontAwesomeIcon icon={faImage} />,
  italic: <FontAwesomeIcon icon={faItalic} />,
  join_up: <FontAwesomeIcon icon={faAngleUp} />,
  lift: <FontAwesomeIcon icon={faOutdent} />,
  link: <FontAwesomeIcon icon={faLink} />,
  ordered_list: <FontAwesomeIcon icon={faListOl} />,
  paragraph: <FontAwesomeIcon icon={faParagraph} />,
  redo: <FontAwesomeIcon icon={faRedo} />,
  strikethrough: <FontAwesomeIcon icon={faStrikethrough} />,
  strong: <FontAwesomeIcon icon={faBold} />,
  subscript: <FontAwesomeIcon icon={faSubscript} />,
  superscript: <FontAwesomeIcon icon={faSuperscript} />,
  table: <FontAwesomeIcon icon={faTable} />,
  underline: <FontAwesomeIcon icon={faUnderline} />,
  undo: <FontAwesomeIcon icon={faUndo} />
};
