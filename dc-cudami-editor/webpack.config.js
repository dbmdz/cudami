const path = require('path');

const config = {
  cache: true,
  devtool: 'sourcemaps',
  entry: './src/lib/CudamiEditor.jsx',
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
    filename: 'cudami-editor.bundle.js',
    library: 'CudamiEditor',
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
