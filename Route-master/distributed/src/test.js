
async function foo() {
	await sleep(1000)
	console.log('a')
}

foo()
console.log('b')
