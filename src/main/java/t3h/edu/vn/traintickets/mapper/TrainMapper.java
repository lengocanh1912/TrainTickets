//package t3h.edu.vn.traintickets.mapper;
//
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//import t3h.edu.vn.traintickets.dto.TrainCreateDto;
//import t3h.edu.vn.traintickets.entities.Train;
//
//@Mapper(componentModel = "spring")
//public interface TrainMapper {
//
//    @Mapping(target = "capacity", expression = "java((short) dto.getCapacity())")
//    Train toEntity(TrainCreateDto dto);
//
//    // Nếu muốn chắc chắn convert đúng
//    default Short mapIntToShort(int value) {
//        return (short) value;
//    }
//}
