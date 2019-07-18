const path = require('path');

module.exports = {
  cache: true,
  devtool: 'sourcemaps',
  entry: './src/index.js',
  mode: 'production',
  module: {
    rules: [
      {
        test: /\.css$/,
        use: ['style-loader', 'css-loader'],
      },
      {
        exclude: /(node_modules)/,
        test: /\.js?$/,
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
    filename: 'editor.bundle.js',
    path: path.join(
      __dirname, '..', 'dc-cudami-admin', 'dc-cudami-admin-webapp',
      'target', 'classes', 'static', 'js'
    )
  },
  resolve: {
    extensions: ['.js'],
  }
}
