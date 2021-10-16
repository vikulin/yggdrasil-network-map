(function() {
	window.demo = {};
	window.demo.data = [];
var data = getData();
for(var t=0;t<1;t++) {
for(var j = 0;j < data.length; j++) {
window.demo.data.push(data[j]);
}
}
function getData() {
return [['fc08:d658:64e7:8b54:5bb0:c926:b463:cfca', 'cfca', '0.4.0.1rc2-33-g76786ea', 'android', 'arm'],
['fc03:da53:fccf:f8ab:4d0e:9702:e3c0:61df', 'mesh.rivchain.org', '0.4.0-13-ga54ac0a', 'linux', 'amd64'],
['fc03:61b9:8377:826f:9e69:10b4:4b8b:ec33', 'ec33', '0.4.0-12-g80b2848', 'windows', 'amd64'],
['fc02:e579:6f63:57ff:475a:cd9e:5068:942f', 'r2.rivchain.org', '0.4.0-13-ga54ac0a', 'linux', 'amd64'],
['fc02:6e0e:6f36:3272:7b8f:ca3b:2789:39d4', 'mesh.rivchain.org', '0.4.0.1rc2-29-g0620327', 'linux', 'amd64'],
['fc02:5c0c:2f62:c1d7:14cb:bfb3:875:f619', 'RiV J', '0.4.0-13-ga54ac0a', 'linux', 'amd64'],
['fc02:11a5:2fb9:6763:4316:42a7:7c56:11a1', 'mm.rivchain.org', '0.4.0.1', 'linux', 'amd64'],
['fc01:97c9:2466:c3c2:52a5:7ff2:73b2:2699', 'r3.rivchain.org', '0.4.0-13-ga54ac0a', 'linux', 'amd64'],
['fc01:4e7a:bf3:4ef9:f4c:ee99:1cb9:a61f', 'раздельная-пк', '0.4.0.1rc2-29-g0620327', 'windows', 'amd64'],
['fc01:23:cac6:14d4:2f33:ffa8:87f4:dac6', 'api2.rivcoin.io', '0.4.0-13-ga54ac0a', 'linux', 'amd64'],
['fc00:eede:5c1e:73ad:3f98:4840:63d:5ba7', 'Server-PC', '0.4.0.1rc2-29-g0620327', 'windows', 'amd64'],
['fc00:2d5:af45:704a:8415:e047:f926:d2c6', 'api1.rivcoin.io', '0.4.0-13-ga54ac0a', 'linux', 'amd64'],
];
}
}());