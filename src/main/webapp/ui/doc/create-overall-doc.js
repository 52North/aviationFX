var fs = require('fs');

var mds = [
  {file: 'introduction.md'},
  {file:'import.md', section :'Importing'},
  {file:'export.md', section :'Exporting'},
  {file:'delete.md', section :'Deleting'},
  {file:'stylesheets.md', section :'Stylesheets'},
  {file:'jobStatus.md', section :'Job Status'}
]

var buffer = '';

mds.forEach(function(md) {
  if (md.section) {
    buffer += '\n\n';
    buffer += '# '+ md.section;
    buffer += '\n\n';
  }

  buffer += fs.readFileSync(md.file, 'utf8').split('${baseUrl}').join('http://rs211980.rs.hosteurope.de/iec');
})

fs.writeFile('api-documentation.md', buffer, 'utf8', function (err) {
  if (err) {
    return console.log(err);
  }
  console.log('md file written');

  var exec = require('child_process').exec;
  var puts = function(error, stdout, stderr) {
    if (err) {
      return console.error('Coult not create ODT');
    }
    console.info('ODT documentation written');
  }

  exec('pandoc -o api-documentation.odt api-documentation.md', puts);
});
