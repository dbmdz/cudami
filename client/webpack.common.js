const path = require('path')
const _ = require('lodash')
const TerserPlugin = require('terser-webpack-plugin')

module.exports = {
  entry: {
    IdentifiableEditor: './src/lib/IdentifiableEditor',
    IdentifiableList: './src/lib/IdentifiableList',
    IdentifierList: './src/lib/IdentifierList',
    IdentifierTypeEditor: './src/lib/IdentifierTypeEditor',
    IdentifierTypeList: './src/lib/IdentifierTypeList',
    LicenseEditor: './src/lib/LicenseEditor',
    LicenseList: './src/lib/LicenseList',
    RenderingTemplateEditor: './src/lib/RenderingTemplateEditor',
    RenderingTemplateList: './src/lib/RenderingTemplateList',
    UrlAliasesList: './src/lib/UrlAliasesList',
    UserEditor: './src/lib/UserEditor',
    UserList: './src/lib/UserList',
  },
  module: {
    rules: [
      {
        test: /\.css$/,
        use: ['style-loader', 'css-loader'],
      },
      {
        exclude: /node_modules/,
        test: /\.tsx?$/,
        use: 'ts-loader',
      },
      {
        exclude: /(node_modules)/,
        test: /\.jsx?$/,
        use: 'babel-loader',
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
        use: 'file-loader',
      },
    ],
  },
  optimization: {
    minimize: true,
    minimizer: [new TerserPlugin({extractComments: false})],
  },
  output: {
    filename: (pathData) => {
      return `${_.kebabCase(pathData.chunk.name)}.bundle.js`
    },
    library: '[name]',
    libraryExport: 'default',
    libraryTarget: 'umd',
    path: path.resolve(
      __dirname,
      '../target/classes/static/js',
    ),
  },
  resolve: {
    extensions: ['.js', '.jsx', '.ts', '.tsx'],
  },
}
