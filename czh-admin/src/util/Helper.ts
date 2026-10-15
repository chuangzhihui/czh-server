

export default class Helper {
	static getNum(index:number, total:number, size:number, page:number, orderBy:string) {
		let num:number | string = index + 1;
		if (orderBy == "desc") {
			num = total - (page - 1) * size - (num - 1);
		} else {
			num = (page - 1) * size + num
		}
		if (num < 10) {
			num = '0' + num;
		}
		return String(num)
	}
	static getNums(value:any) {

		// 只允许输入数字
		value = value.replace(/[^\d]/g, '')
		return value
	}


	static getSex(idcard:string) {
		if (parseInt(idcard.substr(16, 1)) % 2 == 1) {
			return "男"
		} else {
			return "女"
		}
	}
	static getBirth(idcard:string) {
		//获取出生年月日
		const yearBirth = idcard.substring(6, 10);
		const monthBirth = idcard.substring(10, 12);
		const dayBirth = idcard.substring(12, 14);
		const birthday = yearBirth + '-' + monthBirth + '-' + dayBirth;
		return birthday
	}



	static getRandomString(length:number) {
		let result = '';
		const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
		const charactersLength = characters.length;
		for (let i = 0; i < length; i++) {
			result += characters.charAt(Math.floor(Math.random() * charactersLength));
		}
		return result;
	}
	static str2fileList(str:string)
	{
		const imgs=str.split(",");
		const fileList=[];
		for(let i=0;i<imgs.length;i++)
		{
			fileList.push({
				uid:this.getRandomString(12),
				name:this.getRandomString(6),
				url:imgs[i],
				thumbUrl:imgs[i],
				status:"done"
			})
		}
		return fileList;
	}


	static regCheck(regRule:any,value:any){

		const reg=new RegExp(regRule);
		if(!value){
			return false;
		}
		return reg.test(value);
	}

	static getFileSize(size:number){
		let result:string;
		if(size<1024*1024)
		{
			//1M以内
			result=(size/1024).toFixed(2)+"K";
		}else if(size<1024*1024*1024)
		{
			//1G内
			result=(size/(1024*1024)).toFixed(2)+"M";
		}else{
			//G
			result=(size/(1024*1024*1024)).toFixed(2)+"G";
		}
		return result;
	}

	static saveAs(data:any, name:string) {

		const blob = new Blob([data]);

		const aElement = document.createElement('a');

		const blobUrl = window.URL.createObjectURL(blob);

		aElement.href = blobUrl;

		aElement.download = name;

		aElement.click();

		window.URL.revokeObjectURL(blobUrl);
	}
}
