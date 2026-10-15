import "./index.scss"
export interface DrawerMenuItemProps{
    icon:any;
    title:string;
    active?:boolean;
    onClick:()=>void;
}
const DrawerMenuItem=(props:DrawerMenuItemProps)=>{
    return(
        <div onClick={props.onClick} className={`drawerMenuItemContainer ${props.active?"drawerMenuItemContaineractive":""}`}>
            <img className={"icon"} src={props.icon}/>
            <p className={"text"}>{props.title}</p>
        </div>
    );
}
export default DrawerMenuItem;