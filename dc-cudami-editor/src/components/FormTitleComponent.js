import React from 'react';

const FormTitleComponent = (props) => {
  return (
    <h1>
      Website <a href={props.url}>{props.url}</a> bearbeiten
    </h1>
  )
};

export default FormTitleComponent;
