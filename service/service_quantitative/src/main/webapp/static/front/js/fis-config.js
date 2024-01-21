// 清除其他配置，只保留如下配置
fis.match('/markt/*.js', {
    // fis-optimizer-uglify-js 插件进行压缩，已内置
    optimizer: fis.plugin('uglify-js'
        //{
        // 		mangle:true,
        // 		output:{
        // 			beautify:false
        // 		},
        // 		keep_fnames:false,
        // 		compress:{
        // 			booleans:true,
        // 			collapse_vars:true
        // 		}
        // }

    )
});

fis.match('*.css', {
    // fis-optimizer-clean-css 插件进行压缩，已内置
    optimizer: fis.plugin('clean-css')
});

fis.match('*.png', {
    // fis-optimizer-png-compressor 插件进行压缩，已内置
    optimizer: fis.plugin('png-compressor')
});

// fis
//   .media('prod')
//   .match('*.js', {
//     optimizer: fis.plugin('uglify-js', {

//       // https://github.com/mishoo/UglifyJS2#compressor-options
//     })
//   });



