const path = require('path');
const _ = require('lodash');

const config = {
  cache: true,
  devtool: 'sourcemaps',
  entry: {
    GeoLocationsList: './src/lib/GeoLocationsList.jsx',
    IdentifiableEditor: './src/lib/IdentifiableEditor.jsx',
    IdentifiableList: './src/lib/IdentifiableList.jsx',
    PersonsList: './src/lib/PersonsList.jsx',
    RenderingTemplateEditor: './src/lib/RenderingTemplateEditor.jsx',
    RenderingTemplateList: './src/lib/RenderingTemplateList.jsx'
  },
  mode: 'production',
  module: {
    rules: [
      {
        test: /\.css$/,
        use: ['style-loader', 'css-loader'],
      },
      {
        exclude: /(node_modules)/,
        test: /\.jsx?$/,
        use: ['babel-loader']
      },
      {
        test: /\.(png|jpe?g|gif|svg)$/,
        use: {
          loader: 'file-loader',
          options: {
            outputPath: '.',
          },
        },
      },
      {
        test: /\.(woff2|ttf)$/,
        use: ['file-loader']
      },
    ],
  },
  output: {
    filename: (pathData) => {
      return `${_.kebabCase(pathData.chunk.name)}.bundle.js`
    },
    library: '[name]',
    libraryExport: 'default',
    libraryTarget: 'umd',
    path: null
  },
  resolve: {
    extensions: ['.js', '.jsx'],
  }
}

module.exports = (_env, options) => {
  config.output.path = options.outputPath || path.join(__dirname, 'dist');
  return config;
};
